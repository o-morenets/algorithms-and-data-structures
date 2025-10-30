package org.codeus.consistent_hashing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeus.consistent_hashing.consistent_hashing.ConsistentHashing;
import org.codeus.consistent_hashing.dto.*;
import org.codeus.consistent_hashing.util.ProductLoader;
import org.codeus.consistent_hashing.util.StatisticsCollector;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsistentHashingManager {

    private final ProductLoader productLoader;
    private final StatisticsCollector statisticsCollector;
    private ConsistentHashing<Product> consistentHashing;

    public InitResponse initializeHashing(int nodeCount) {
        log.info("Initializing with {} nodes", nodeCount);

        try {
            List<Product> products = productLoader.loadFromJson();

            // TODO: Create a new instance of ConsistentHashing using Product::getId as key extractor
            consistentHashing = new ConsistentHashing<>(Product::getId);

            // TODO: Initialize the ring with the given number of nodes (check ConsistentHashing for a method that configures node count).
            consistentHashing.setNodeCount(nodeCount);

            // TODO: Add all elements to the cashing using the appropriate method from ConsistentHashing
            consistentHashing.addElements(products);

            // TODO: Return InitResponse with proper data from ConsistentHashing (node count, total elements, checked elements, and distribution)
            return InitResponse.builder()
                    .message("Initialized with " + nodeCount + " nodes")
                    .nodeCount(nodeCount)
                    .totalProducts(consistentHashing.getTotalElements())
                    .checkedElements(consistentHashing.getCheckedElements())
                    .distribution(statisticsCollector.getDistribution(consistentHashing))
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load products", e);
        }
    }

    public AddNodeResponse addNode() {
        validateInitialized();
        log.info("Adding new node");

        // TODO: Capture the current distribution before modification using ConsistentHashing
        Map<String, String> oldDistribution = consistentHashing.captureDistribution();

        // TODO: Use the method from ConsistentHashing to add a new node to the ring
        consistentHashing.addNode();

        // TODO: After assigning a value to 'oldDistribution', uncomment the following lines
        List<NodeMovementSummary> movements = statisticsCollector.trackNodeMovements(consistentHashing, oldDistribution);
        int moved = statisticsCollector.countMovedElements(movements);

        log.info("Node added. Checked: {}, Moved: {}", consistentHashing.getCheckedElements(), moved);

        // TODO: Return AddNodeResponse with relevant data (node count, checked elements, moved elements, distribution, movements)
        return AddNodeResponse.builder()
                .message("Node added successfully")
                .nodeCount(consistentHashing.getNodeCount())
                .checkedElements(consistentHashing.getCheckedElements())
                .movedElements(moved)
                .distribution(statisticsCollector.getDistribution(consistentHashing))
                .movements(movements)
                .build();
    }

    public RemoveNodeResponse removeNode(int nodeIndex) {
        validateInitialized();
        log.info("Removing node at index {}", nodeIndex);

        // TODO: Capture the current distribution before removal using ConsistentHashing
        Map<String, String> oldDistribution = consistentHashing.captureDistribution();

        // TODO: Use the correct method from ConsistentHashing to remove a node by its index
        consistentHashing.removeNodeByIndex(nodeIndex);

        // TODO: After assigning a value to 'oldDistribution', uncomment the following lines
        List<NodeMovementSummary> movements = statisticsCollector.trackNodeMovements(consistentHashing, oldDistribution);
        int moved = statisticsCollector.countMovedElements(movements);

        log.info("Node removed. Checked: {}, Moved: {}", consistentHashing.getCheckedElements(), moved);

        // TODO: Return RemoveNodeResponse with relevant data (node count, checked elements, moved elements, distribution, movements)
        return RemoveNodeResponse.builder()
                .message("Node removed successfully")
                .nodeCount(consistentHashing.getNodeCount())
                .checkedElements(consistentHashing.getCheckedElements())
                .movedElements(moved)
                .distribution(statisticsCollector.getDistribution(consistentHashing))
                .movements(movements)
                .build();
    }

    private void validateInitialized() {
        if (consistentHashing == null) {
            throw new IllegalStateException("Not initialized. Call /api/init first");
        }
    }
}