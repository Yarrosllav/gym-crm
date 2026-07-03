package com.gym.dao;

import com.gym.dao.impl.TrainingDAOImpl;
import com.gym.db.Storage;
import com.gym.model.Training;
import com.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TrainingDAOTest {

    private TrainingDAO trainingDAO;

    private Storage storage;

    private Map<UUID, Training> trainingsMap;

    @BeforeEach
    void setUp() {
        trainingsMap = new HashMap<>();
        storage = new Storage(new HashMap<>(), trainingsMap, new HashMap<>());
        trainingDAO = new TrainingDAOImpl(storage);
    }

    @Test
    void testCreate_ShouldAddTrainingsToMapAndGenerateId() {
        var training = new Training();
        training.setTrainingName("Test Training");
        training.setTrainingType(TrainingType.HIIT);
        training.setTrainingDuration(60.0);

        trainingDAO.create(training);

        assertEquals(1, trainingsMap.size());

        assertNotNull(training.getTrainingId());

        var createdTraining = trainingsMap.get(training.getTrainingId());
        assertEquals("Test Training", createdTraining.getTrainingName());
    }

    @Test
    void testFindById_ShouldReturnTraining() {
        var id = UUID.randomUUID();
        var training = new Training();
        training.setTrainingName("Test Training");
        training.setTrainingType(TrainingType.HIIT);
        training.setTrainingDuration(60.0);

        trainingsMap.put(id, training);

        var result = trainingDAO.findById(id);

        assertNotNull(result);
        assertEquals("Test Training", result.getTrainingName());
    }
}
