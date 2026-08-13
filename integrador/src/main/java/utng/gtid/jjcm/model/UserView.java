package utng.gtid.jjcm.model;

import java.time.LocalDateTime;

/**
 * Usuario completo utilizado por las pantallas Usuarios y Mi perfil.
 */
public final class UserView {

    /** Identificador único de PostgreSQL. */
    private final long id;

    /** Identificador del rol asignado. */
    private final long roleId;

    /** Nombre legible del rol. */
    private final String role;

    /** Nombre o nombres de la persona. */
    private final String name;

    /** Apellidos de la persona. */
    private final String lastName;

    /** Correo institucional único. */
    private final String email;

    /** Teléfono de contacto; puede estar vacío. */
    private final String phone;

    /** Puesto institucional; puede estar vacío. */
    private final String position;

    /** Área o departamento; puede estar vacío. */
    private final String department;

    /** Indica si el usuario puede utilizarse en operaciones nuevas. */
    private final boolean active;

    /** Última fecha de acceso; puede ser null si nunca inició sesión. */
    private final LocalDateTime lastAccess;

    /** Construye una vista inmutable a partir de una fila SQL. */
    public UserView(
            long id,
            long roleId,
            String role,
            String name,
            String lastName,
            String email,
            String phone,
            String position,
            String department,
            boolean active,
            LocalDateTime lastAccess
    ) {
        this.id = id;
        this.roleId = roleId;
        this.role = role;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.position = position;
        this.department = department;
        this.active = active;
        this.lastAccess = lastAccess;
    }

    /** @return identificador del usuario. */
    public long getId() { return id; }

    /** @return identificador del rol. */
    public long getRoleId() { return roleId; }

    /** @return rol visible. */
    public String getRole() { return role; }

    /** @return nombre. */
    public String getName() { return name; }

    /** @return apellidos. */
    public String getLastName() { return lastName; }

    /** @return nombre completo. */
    public String getFullName() { return name + " " + lastName; }

    /** @return correo. */
    public String getEmail() { return email; }

    /** @return teléfono. */
    public String getPhone() { return phone; }

    /** @return puesto. */
    public String getPosition() { return position; }

    /** @return departamento. */
    public String getDepartment() { return department; }

    /** @return true cuando está activo. */
    public boolean isActive() { return active; }

    /** @return último acceso o null. */
    public LocalDateTime getLastAccess() { return lastAccess; }
}
