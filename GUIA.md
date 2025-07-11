## 🎯 OBJETIVO

Mostrar a un estudiante un ejercicio adaptado a su nivel y competencia, renderizado con la plantilla adecuada, generado dinámicamente con IA (Azure AI Foundry), y registrado su desempeño.

---

## 🧩 PASO A PASO DEL FLUJO COMPLETO

---

### 1. 📥 INICIO DE SOLICITUD DE EJERCICIO

* *Actor:* Estudiante inicia un Learning Point.
* *Cliente (Angular)* hace GET /api/student/{id}/learning-point/{lp_id}/next-exercise
* *Parámetros clave:*

  * student_id
  * learning_point_id
  * competency_id (se asocia al LP)
  * difficulty_level (calculado con su desempeño anterior)
  * exercise_type_id (definido en el LP o por sistema adaptativo)

---

### 2. 🤖 DECISIÓN EN BACKEND (Spring Boot)

#### A. Determinar qué tipo de ejercicio presentar

* Se consulta la tabla exercise_type
* Se decide el tipo de plantilla visual (ej. drag_and_drop)
* Se asocia con una plantilla de *prompt AI*

#### B. Construcción del prompt dinámico

* Se selecciona una prompt_template con placeholders:

  text
  Genera un ejercicio de nivel {{nivel}} para la competencia {{competencia}}, del tipo {{tipo}}, con una consigna clara y opciones plausibles.
  
* Se reemplazan dinámicamente con los valores:

  * nivel = intermedio
  * competencia = "Uso de fracciones en resolución de problemas"
  * tipo = multiple_choice

#### C. Llamada a Azure AI Foundry

* Se hace POST /azure/prompt-engine con el prompt generado
* Azure devuelve JSON con:

  * title, instructions, options, correct_answer, explanation, image_url (opcional)

---

### 3. 📦 FORMATO DE RESPUESTA BACKEND → FRONTEND

Se arma una respuesta completa como esta:

json
{
  "exercise_id": null,
  "exercise_type": "drag_and_drop",
  "render_variant": "drag-to-sort",
  "difficulty_level": "intermedio",
  "config": {
    "show_timer": true,
    "max_time": 90,
    "allow_partial_score": true,
    "item_count": 5,
    "shuffle_items": true
  },
  "content": {
    "title": "Ordena los pasos de una receta",
    "instructions": "Arrastra los pasos en el orden correcto.",
    "items": ["Romper huevos", "Agregar sal", "Batir", "Freír", "Servir"],
    "correct_order": [0, 1, 2, 3, 4]
  }
}


---

### 4. 🎨 FRONTEND RENDERIZA CON COMPONENTE CORRESPONDIENTE

#### A. Angular recibe el JSON

* Lee exercise_type y render_variant
* Usa un switch o component mapper para cargar dinámicamente el componente:
  DragAndDropComponent

#### B. El componente:

* Usa el campo config para ajustar su comportamiento:

  * Mostrar timer
  * Mostrar hints
  * Activar retroalimentación inmediata
* Muestra el contenido generado (title, items, etc.)

---

### 5. 📝 REGISTRO DEL INTENTO

Cuando el estudiante responde:

* Se envía POST /api/exercise-attempt

  * student_profile_id
  * excercise_id (si fue guardado previamente, o se guarda en ese momento)
  * attempt_number
  * is_correct
  * points_earned (calculado con tiempo y dificultad)
  * time_spent

Este intento se registra en exercise_attempt y contribuye a:

* student_performance_snapshot
* Evaluación de intervención adaptativa (adaptive_intervention)
* Cálculo de siguiente nivel (difficulty_adjustment en learning_path)

---

### 6. 🔁 ADAPTACIÓN AUTOMÁTICA

* El backend evalúa desempeño con métricas:

  * Tasa de éxito
  * Tiempo promedio
  * Número de intentos
  * Retroalimentación del alumno

* Ajusta en el próximo ejercicio:

  * difficulty_level: puede subir o bajar
  * exercise_type: puede variar si el alumno necesita variedad
  * intervention: si el alumno falla varias veces, se genera una intervención

---

## 📦 ¿Dónde viven las cosas?

| Elemento                | Almacenamiento                 |
| ----------------------- | ------------------------------ |
| Prompt templates (AI)   | Base de datos o JSON backend   |
| Exercise types (UI)     | Frontend Angular (componentes) |
| Render variants/configs | En el JSON retornado           |
| Ejercicios generados    | En memoria o base de datos     |
| Respuestas/intentos     | Tabla exercise_attempt       |