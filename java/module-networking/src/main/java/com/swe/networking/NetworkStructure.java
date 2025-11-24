/*
 * -----------------------------------------------------------------------------
 *  File: NetworkStructure.java
 *  Owner: Vishwaa
 *  Roll Number : 112201030
 *  Module : Networking
 *
 * -----------------------------------------------------------------------------
 */
package com.swe.networking;

import java.util.List;

import com.swe.core.ClientNode;

public record NetworkStructure(List<List<ClientNode>> clusters, List<ClientNode> servers) {
}
