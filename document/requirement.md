# Requirement

Add nw functionality to support video registration and listing. This feature should contain the following functionalities:

- Video registration: Users should be able to register videos metadata to the platform. Each video should have a unique identifier, titulo, fecha de creacion, duracion, reproduciones, descripcion, formato, url, categoria.
- Video listing: Users should be able to view a list of all registered videos. The list should include the video title, description, tags, and other relevant metadata.
- Since this project follows MVC architecture, at Vista create 2 .jsp files: registroVid.jsp and listadoVid.jsp, at controller create 2 classes: servletRegistroVid and servletListadoVid and finally at model create 1 class: video.java.

## Requirement 2

After implementing the video registration and listing feature, refactor the codebase to follow:

- Clean separation of concerns: Ensure that the code is well-structured, with distinct layers for the view, controller, and model.
- Use of design patterns: Apply appropriate design patterns, such as the Model-View-Controller (MVC) pattern, to improve code organization and maintainability.
- Create new classes if necessary to encapsulate specific functionality or to adhere to the MVC pattern.
- Use of specific database access classes: Create separate classes to handle database operations, such as video registration and listing. These classes should encapsulate the database logic and provide a clean interface for the controller to interact with.
- Ensure correct error handling with centralized error handling mechanism.
- All form inputs should be validated and handled properly to prevent invalid data from being processed. And display appropriate error messages to the user allow them to correct the input.