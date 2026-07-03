package com.gym.config;

import com.gym.db.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageInitPostProcessorTest {

    @Mock
    private Resource dataFile;

    @InjectMocks
    private StorageInitPostProcessor postProcessor;

    private Storage storage;

    @BeforeEach
    void setUp() {
        storage = new Storage(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>()
        );
    }

    @Test
    void testPostProcessAfterInit_WithValidData() throws Exception {
        var mockCsvData = "TRAINEE,John,Doe,1990-01-01,New York\n" +
                "TRAINER,Jane,Smith,YOGA\n";

        var inputStream = new ByteArrayInputStream(mockCsvData.getBytes());

        when(dataFile.exists()).thenReturn(true);
        when(dataFile.getInputStream()).thenReturn(inputStream);

        var result = postProcessor.postProcessAfterInitialization(storage, "storageBean");

        assertEquals(storage, result);

        assertEquals(1, storage.getAllTrainees().size());
        assertEquals(1, storage.getAllTrainers().size());

        var savedTrainee = storage.getAllTrainees().values().iterator().next();
        var savedTrainer = storage.getAllTrainers().values().iterator().next();

        assertEquals("John", savedTrainee.getFirstName());
        assertEquals("YOGA", savedTrainer.getSpecialization());

        assertNotNull(savedTrainee.getUserId());
        assertNotNull(savedTrainer.getUserId());
    }

    @Test
    void testPostProcessAfterInit_WithOtherBean_ShouldDoNothing() {
        var someBean = new Object();
        var result = postProcessor.postProcessAfterInitialization(someBean, "someBean");

        assertEquals(someBean, result);
    }
}
