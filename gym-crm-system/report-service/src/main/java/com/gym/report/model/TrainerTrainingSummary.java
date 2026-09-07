package com.gym.report.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_training_summary")
@CompoundIndex(name = "idx_first_last_name", def = "{'first_name': 1, 'last_name': 1}")
@Getter
@Setter
public class TrainerTrainingSummary {

    private String id;

    @Version
    private Long version;

    @Indexed(unique = true)
    @Field("trainer_username")
    private String trainerUsername;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("trainer_status")
    private boolean trainerStatus;

    private List<YearSummary> years = new ArrayList<>();
}
