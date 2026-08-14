package com.example.codeintel.dto;

import java.util.List;

public record ImpactAnalysisResponse(String rootFqn, String direction, int depth, List<String> affectedNodes) {

}
