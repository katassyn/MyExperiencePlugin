package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Performance monitoring system for skill effect handlers
 */
public class PerformanceMonitor {
    
    private static final int MAX_SAMPLES = 1000;
    
    // Method name -> execution times (nanoseconds)
    private final Map<String, List<Long>> executionTimes = new ConcurrentHashMap<>();
    
    // Method name -> call count
    private final Map<String, AtomicLong> callCounts = new ConcurrentHashMap<>();
    
    // Method name -> total time (nanoseconds)  
    private final Map<String, AtomicLong> totalTimes = new ConcurrentHashMap<>();
    
    // Skill purchase cache: Player UUID -> Set<SkillID>
    private final Map<UUID, Set<Integer>> skillPurchaseCache = new ConcurrentHashMap<>();
    
    // Cache expiry: Player UUID -> Expiry Time
    private final Map<UUID, Long> cacheExpiry = new ConcurrentHashMap<>();
    
    // Player attributes cache: Player UUID -> AttributeType -> CachedValue
    private final Map<UUID, Map<String, CachedValue>> attributeCache = new ConcurrentHashMap<>();
    
    // Cache hit/miss stats
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    private final MyExperiencePlugin plugin;
    
    // Cache TTL (5 minutes for skills, 30 seconds for attributes)
    private static final long SKILL_CACHE_TTL = 300_000L;
    private static final long ATTRIBUTE_CACHE_TTL = 30_000L;
    
    public PerformanceMonitor(MyExperiencePlugin plugin) {
        this.plugin = plugin;
        
        // Schedule cache cleanup every minute
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
            this::cleanupExpiredCaches, 1200L, 1200L);
    }
    
    /**
     * Track method execution time
     */
    public <T> T trackExecution(String methodName, java.util.function.Supplier<T> method) {
        long startTime = System.nanoTime();
        try {
            T result = method.get();
            return result;
        } finally {
            long duration = System.nanoTime() - startTime;
            recordExecution(methodName, duration);
        }
    }
    
    /**
     * Track method execution time (void methods)
     */
    public void trackExecution(String methodName, Runnable method) {
        long startTime = System.nanoTime();
        try {
            method.run();
        } finally {
            long duration = System.nanoTime() - startTime;
            recordExecution(methodName, duration);
        }
    }
    
    /**
     * Record execution time for method
     */
    private void recordExecution(String methodName, long duration) {
        // Update call count
        callCounts.computeIfAbsent(methodName, k -> new AtomicLong(0)).incrementAndGet();
        
        // Update total time
        totalTimes.computeIfAbsent(methodName, k -> new AtomicLong(0)).addAndGet(duration);
        
        // Store individual execution time (with sampling)
        List<Long> times = executionTimes.computeIfAbsent(methodName, k -> Collections.synchronizedList(new ArrayList<>()));
        times.add(duration);
        
        // Keep only recent samples
        if (times.size() > MAX_SAMPLES) {
            times.remove(0);
        }
    }
    
    /**
     * Get cached skill purchases for player (with cache management)
     */
    public Set<Integer> getCachedSkillPurchases(UUID playerId) {
        long now = System.currentTimeMillis();
        
        // Check if cache is still valid
        if (cacheExpiry.getOrDefault(playerId, 0L) > now) {
            cacheHits.incrementAndGet();
            return skillPurchaseCache.getOrDefault(playerId, Collections.emptySet());
        }
        
        // Cache miss - refresh from database
        cacheMisses.incrementAndGet();
        refreshSkillCache(playerId);
        return skillPurchaseCache.getOrDefault(playerId, Collections.emptySet());
    }
    
    /**
     * Refresh skill cache for player
     */
    private void refreshSkillCache(UUID playerId) {
        Set<Integer> skills = plugin.getSkillTreeManager().getPurchasedSkills(playerId);
        skillPurchaseCache.put(playerId, new HashSet<>(skills));
        cacheExpiry.put(playerId, System.currentTimeMillis() + SKILL_CACHE_TTL);
    }
    
    /**
     * Cached attribute value with expiry
     */
    public static class CachedValue {
        private final double value;
        private final long expiryTime;
        
        public CachedValue(double value, long ttlMs) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttlMs;
        }
        
        public double getValue() { return value; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
    
    /**
     * Get cached attribute value for player
     * @param playerId Player UUID
     * @param attributeKey Key like "maxHealth", "currentHealth", etc.
     * @return Cached value or null if expired/missing
     */
    public Double getCachedAttribute(UUID playerId, String attributeKey) {
        Map<String, CachedValue> playerCache = attributeCache.get(playerId);
        if (playerCache == null) {
            return null;
        }
        
        CachedValue cached = playerCache.get(attributeKey);
        if (cached == null || cached.isExpired()) {
            // Remove expired entry
            if (cached != null) {
                playerCache.remove(attributeKey);
                if (playerCache.isEmpty()) {
                    attributeCache.remove(playerId);
                }
            }
            return null;
        }
        
        cacheHits.incrementAndGet();
        return cached.getValue();
    }
    
    /**
     * Cache an attribute value for player
     * @param playerId Player UUID
     * @param attributeKey Key like "maxHealth", "currentHealth", etc.
     * @param value The value to cache
     */
    public void cacheAttribute(UUID playerId, String attributeKey, double value) {
        attributeCache.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                    .put(attributeKey, new CachedValue(value, ATTRIBUTE_CACHE_TTL));
    }
    
    /**
     * Invalidate cache for player (call when skills change)
     */
    public void invalidateSkillCache(UUID playerId) {
        skillPurchaseCache.remove(playerId);
        cacheExpiry.remove(playerId);
        attributeCache.remove(playerId);
    }
    
    /**
     * Cleanup expired caches
     */
    private void cleanupExpiredCaches() {
        long now = System.currentTimeMillis();
        
        // Clean up skill caches
        cacheExpiry.entrySet().removeIf(entry -> {
            if (entry.getValue() <= now) {
                skillPurchaseCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
        
        // Clean up expired attribute caches
        attributeCache.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            Map<String, CachedValue> playerCache = entry.getValue();
            
            // Remove expired values from player's attribute cache
            playerCache.entrySet().removeIf(attrEntry -> attrEntry.getValue().isExpired());
            
            // If player has no more cached attributes, remove the player entry
            return playerCache.isEmpty();
        });
    }
    
    /**
     * Get performance statistics
     */
    public Map<String, MethodStats> getPerformanceStats() {
        Map<String, MethodStats> stats = new HashMap<>();
        
        for (String method : callCounts.keySet()) {
            long calls = callCounts.get(method).get();
            long totalTime = totalTimes.get(method).get();
            List<Long> times = executionTimes.get(method);
            
            if (calls > 0) {
                double avgTime = (double) totalTime / calls;
                
                // Calculate percentiles
                List<Long> sortedTimes = times.stream().sorted().collect(Collectors.toList());
                long p50 = getPercentile(sortedTimes, 0.50);
                long p90 = getPercentile(sortedTimes, 0.90);
                long p99 = getPercentile(sortedTimes, 0.99);
                long max = sortedTimes.isEmpty() ? 0 : sortedTimes.get(sortedTimes.size() - 1);
                
                stats.put(method, new MethodStats(method, calls, avgTime, p50, p90, p99, max));
            }
        }
        
        return stats;
    }
    
    /**
     * Get cache statistics
     */
    public CacheStats getCacheStats() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total : 0.0;
        
        return new CacheStats(hits, misses, hitRate, skillPurchaseCache.size(), cacheExpiry.size());
    }
    
    /**
     * Get percentile from sorted list
     */
    private long getPercentile(List<Long> sortedTimes, double percentile) {
        if (sortedTimes.isEmpty()) return 0;
        
        int index = (int) Math.ceil(percentile * sortedTimes.size()) - 1;
        index = Math.max(0, Math.min(index, sortedTimes.size() - 1));
        return sortedTimes.get(index);
    }
    
    /**
     * Reset all performance statistics
     */
    public void reset() {
        executionTimes.clear();
        callCounts.clear();
        totalTimes.clear();
        cacheHits.set(0);
        cacheMisses.set(0);
    }
    
    /**
     * Display performance report to command sender
     */
    public void displayReport(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Skill Effects Performance Report ===");
        
        // Performance stats
        Map<String, MethodStats> stats = getPerformanceStats();
        if (!stats.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Method Performance (times in microseconds):");
            
            stats.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue().avgTime, a.getValue().avgTime))
                .limit(10)
                .forEach(entry -> {
                    MethodStats s = entry.getValue();
                    sender.sendMessage(String.format("%s%s: %s%,d calls, %s%.1f μs avg, %s%.1f/%.1f/%.1f μs (50/90/99%%)",
                        ChatColor.WHITE, s.methodName,
                        ChatColor.GRAY, s.callCount,
                        ChatColor.GREEN, s.avgTime / 1000.0,
                        ChatColor.AQUA, s.p50 / 1000.0, s.p90 / 1000.0, s.p99 / 1000.0));
                });
        }
        
        // Cache stats
        CacheStats cacheStats = getCacheStats();
        sender.sendMessage(ChatColor.YELLOW + "Cache Performance:");
        sender.sendMessage(String.format("%sHits: %s%,d, %sMisses: %s%,d, %sHit Rate: %s%.1f%%",
            ChatColor.WHITE, ChatColor.GREEN, cacheStats.hits,
            ChatColor.WHITE, ChatColor.RED, cacheStats.misses,
            ChatColor.WHITE, ChatColor.GOLD, cacheStats.hitRate * 100));
        sender.sendMessage(String.format("%sCached Players: %s%,d, %sExpiry Entries: %s%,d",
            ChatColor.WHITE, ChatColor.GRAY, cacheStats.cachedPlayers,
            ChatColor.WHITE, ChatColor.GRAY, cacheStats.expiryEntries));
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        sender.sendMessage(ChatColor.YELLOW + "Memory Usage:");
        sender.sendMessage(String.format("%sUsed: %s%.1f MB, %sTotal: %s%.1f MB, %sFree: %s%.1f MB",
            ChatColor.WHITE, ChatColor.RED, usedMemory / 1024.0 / 1024.0,
            ChatColor.WHITE, ChatColor.GRAY, totalMemory / 1024.0 / 1024.0,
            ChatColor.WHITE, ChatColor.GREEN, freeMemory / 1024.0 / 1024.0));
    }
    
    /**
     * Method performance statistics
     */
    public static class MethodStats {
        public final String methodName;
        public final long callCount;
        public final double avgTime;
        public final long p50, p90, p99, max;
        
        public MethodStats(String methodName, long callCount, double avgTime, 
                          long p50, long p90, long p99, long max) {
            this.methodName = methodName;
            this.callCount = callCount;
            this.avgTime = avgTime;
            this.p50 = p50;
            this.p90 = p90;
            this.p99 = p99;
            this.max = max;
        }
    }
    
    /**
     * Cache performance statistics
     */
    public static class CacheStats {
        public final long hits, misses;
        public final double hitRate;
        public final int cachedPlayers, expiryEntries;
        
        public CacheStats(long hits, long misses, double hitRate, int cachedPlayers, int expiryEntries) {
            this.hits = hits;
            this.misses = misses;
            this.hitRate = hitRate;
            this.cachedPlayers = cachedPlayers;
            this.expiryEntries = expiryEntries;
        }
    }
}