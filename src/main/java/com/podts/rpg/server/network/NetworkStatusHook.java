package com.podts.rpg.server.network;

import java.util.function.BiConsumer;

import com.podts.rpg.server.network.NetworkManager.NetworkStatus;

@FunctionalInterface
public interface NetworkStatusHook extends BiConsumer<NetworkStatus, NetworkStatus> {
	
}
