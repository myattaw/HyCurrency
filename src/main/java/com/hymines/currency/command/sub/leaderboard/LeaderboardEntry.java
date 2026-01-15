package com.hymines.currency.command.sub.leaderboard;

import java.math.BigDecimal;

record LeaderboardEntry(String playerId, BigDecimal amount) {}