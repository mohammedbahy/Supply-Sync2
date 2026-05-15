package com.supplysync.presentation;

import com.supplysync.facade.ApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext app = ApplicationContext.createDefault();
        UI ui = new UI(app);
        ui.run();
    }
}
