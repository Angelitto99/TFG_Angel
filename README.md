Proyecto Final de Grado – Ciclo DAM  
Autor: Ángel Luis Martínez Fernández  
Título: ServisurTelecomunicaciones App  
Curso: 2024–2025  

───────────────────────────────────────────────────────────  
Descripción  
───────────────────────────────────────────────────────────  
ServisurTelecomunicaciones es una aplicación móvil desarrollada como parte del Trabajo de Fin de Grado (Ciclo DAM).  
Está diseñada para facilitar la comunicación y gestión interna entre una empresa real del sector de telecomunicaciones y sus clientes.  
La aplicación permite a los usuarios registrar incidencias, consultar facturas y presupuestos, recibir notificaciones personalizadas y gestionar su perfil.  
Los administradores, por su parte, pueden gestionar avisos, subir documentación y controlar el acceso mediante PIN.  
Toda la información se sincroniza en tiempo real utilizando Firebase, garantizando acceso remoto, rápido y seguro.


───────────────────────────────────────────────────────────  
Funcionalidades  
───────────────────────────────────────────────────────────  
Cliente:  
- Registro e inicio de sesión  
- Edición de perfil  
- Consulta de facturas y presupuestos  
- Envío de incidencias con imágenes  
- Recepción de notificaciones  
- Opción de recuperar contraseña  
- Visualización de toasts personalizados con logo

Administrador:  
- Acceso mediante PIN (correo autorizado + PIN)  
- Gestión de incidencias recibidas  
- Subida de presupuestos  
- Subida y organización de facturas  
- Consulta de usuarios registrados

Invitado:  
- Acceso sin registro con funciones limitadas

───────────────────────────────────────────────────────────  
Estructura del proyecto  
───────────────────────────────────────────────────────────  
/activities → Pantallas principales (login, perfil, inicio…)  
/model      → Clases de datos (usuario, factura, incidencia…)  
/adapters   → Adaptadores para listas y vistas dinámicas  
/utils      → Funciones auxiliares (toasts personalizados, validaciones…)  
/layout     → Archivos XML con la interfaz de usuario  
/firebase   → Conexión a Firestore, Authentication y Storage

───────────────────────────────────────────────────────────  
Tecnologías utilizadas  
───────────────────────────────────────────────────────────  
- Android Studio  
- Kotlin  
- Firebase (Authentication, Firestore, Storage)  
- Glide (carga de imágenes)  
- CircleImageView (avatares redondos)

───────────────────────────────────────────────────────────  
Limitaciones actuales  
───────────────────────────────────────────────────────────  
- La función “¿Ha olvidado su contraseña?” no finaliza el flujo correctamente.  
  Redirige a una cuenta vacía por limitaciones con Firebase.

───────────────────────────────────────────────────────────  
Cómo usar la aplicación  
───────────────────────────────────────────────────────────  
1. Instala el APK en un dispositivo Android.  
2. Inicia sesión como cliente, invitado o administrador (con PIN).  
3. Navega por las distintas secciones: perfil, incidencias, presupuestos, facturas.  
4. Los datos se sincronizan automáticamente con Firebase.  
5. Las incidencias también son redirigidas por correo electrónico a la empresa para mejorar el seguimiento.

───────────────────────────────────────────────────────────  
Recursos adicionales  
───────────────────────────────────────────────────────────  
Repositorio GitHub:  
https://github.com/Angelitto99/TFG_Angel  

Vídeo demostrativo:  
https://youtu.be/OJIjHhavr3M?si=3BT5Cz9t_DKnavXr

───────────────────────────────────────────────────────────  
Contacto  
───────────────────────────────────────────────────────────  
Correo: angelluismartinezfernandez@gmail.com
