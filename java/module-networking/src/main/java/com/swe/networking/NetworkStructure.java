package com.swe.networking;

import com.swe.core.ClientNode;
import java.util.List;

public record NetworkStructure(List<List<ClientNode>> clusters, List<ClientNode> servers) {
}
