package com.example.mealprep.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Recipe {
    private String title;
    private int id;
    private String image;
    private int servings;
    private int readyInMinuted;
    private String sourceUrl;
    private List<Object> nutrients;
    private List<String> instructions;
}
