package org.example;

import org.example.ui.MainMenu;

public class BoardApplication {

    public static void main(String[] args) {

        System.out.println("Iniciando o Gerenciador de Boards...");

        MainMenu menu = new MainMenu();

        try {
            menu.execute();
        } catch (Exception e) {
            System.err.println("Ocorreu um erro crítico na aplicação: " + e.getMessage());
        } finally {
            System.out.println("Aplicação encerrada.");
        }
    }
}