package com.gestion_de_seguridad.shared.domain;

import java.util.regex.Pattern;

/**
 * Utilidades de validacion de entrada de usuario.
 *
 * Centraliza las reglas de validacion para que toda informacion que ingrese
 * al sistema (formularios, busquedas) sea verificada antes de persistirse,
 * evitando errores en BD y entradas maliciosas o malformadas.
 */
public final class Validacion {

    private static final Pattern SOLO_LETRAS_ESPACIOS = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$");
    private static final Pattern DNI = Pattern.compile("^\\d{7,8}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validacion() {
    }

    /**
     * Verifica que un texto no sea null ni vacio (tras recortar espacios).
     *
     * @throws ValidacionException si el texto esta vacio
     */
    public static String requerido(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    /**
     * Limita la longitud maxima de un texto.
     *
     * @throws ValidacionException si excede el maximo
     */
    public static String longitud(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new ValidacionException(
                    "El campo '" + campo + "' excede el maximo de " + maximo + " caracteres.");
        }
        return valor;
    }

    /**
     * Valida un DNI (7 u 8 digitos numericos).
     */
    public static String dni(String valor) {
        String normalizado = requerido(valor, "DNI");
        if (!DNI.matcher(normalizado).matches()) {
            throw new ValidacionException(
                    "El DNI debe contener entre 7 y 8 digitos numericos (sin puntos ni espacios).");
        }
        return normalizado;
    }

    /**
     * Valida un email corporativo.
     */
    public static String email(String valor, String campo) {
        String normalizado = requerido(valor, campo);
        if (!EMAIL.matcher(normalizado).matches()) {
            throw new ValidacionException("El campo '" + campo + "' no tiene un formato de email valido.");
        }
        return normalizado;
    }

    /**
     * Valida que un texto solo contenga letras y espacios (nombres, puestos, etc.).
     */
    public static String soloLetras(String valor, String campo) {
        String normalizado = requerido(valor, campo);
        if (!SOLO_LETRAS_ESPACIOS.matcher(normalizado).matches()) {
            throw new ValidacionException(
                    "El campo '" + campo + "' solo puede contener letras y espacios (sin numeros ni simbolos).");
        }
        return normalizado;
    }

    /**
     * Rechaza texto que pueda contener caracteres de control o tags que
     * sugieran intento de inyeccion (defensa en profundidad; la proteccion
     * principal contra SQL injection son los prepared statements).
     */
    public static String sinCaracteresPeligrosos(String valor, String campo) {
        String normalizado = requerido(valor, campo);
        if (normalizado.indexOf('\0') >= 0 || normalizado.indexOf(';') >= 0
                || normalizado.toLowerCase().startsWith("select ")
                || normalizado.toLowerCase().startsWith("insert ")
                || normalizado.toLowerCase().startsWith("drop ")) {
            throw new ValidacionException("El campo '" + campo + "' contiene caracteres no permitidos.");
        }
        return normalizado;
    }

    /**
     * Valida que un numero entero este dentro de un rango (si se necesita).
     */
    public static int rango(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo) {
            throw new ValidacionException(
                    "El campo '" + campo + "' debe estar entre " + minimo + " y " + maximo + ".");
        }
        return valor;
    }
}
