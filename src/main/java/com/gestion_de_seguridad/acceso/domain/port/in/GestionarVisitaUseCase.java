package com.gestion_de_seguridad.acceso.domain.port.in;

import com.gestion_de_seguridad.acceso.domain.model.EstrategiaCreacionVisita;
import com.gestion_de_seguridad.acceso.domain.model.Visita;

import java.util.List;

/**
 * Caso de uso de entrada: gestion de visitas y control de acceso.
 *
 * Resuelve los flujos de la seccion 2: pre-registrada, no anunciada, pase
 * temporal por olvido de carnet, y regularizacion por salida olvidada.
 * Define el uso de la estrategia de creacion (patron Strategy).
 */
public interface GestionarVisitaUseCase {

    /**
     * Crea una visita aplicando la estrategia de flujo indicada.
     *
     * @param idUsuario  usuario que registra (permiso 'crear_visita')
     * @param estrategia tipo de flujo (pre-registrada, no anunciada, olvido de carnet)
     * @param datos      datos de la visita (sin estado)
     */
    Visita crearVisita(Long idUsuario, EstrategiaCreacionVisita estrategia, Visita datos);

    /**
     * Aprueba una visita pendiente (permiso 'aprobar_visita').
     */
    Visita aprobar(Long idFuncionario, Long idVisita);

    /**
     * Rechaza una visita pendiente (permiso 'rechazar_visita').
     */
    Visita rechazar(Long idFuncionario, Long idVisita);

    /**
     * Registra el ingreso de la visita. Si la persona tiene una visita previa
     * aun activa (salida olvidada), la cierra por sistema antes de continuar.
     *
     * @param idGuarda  usuario guarda (permiso 'registrar_checkin')
     * @param idVisita  visita a registrar
     */
    Visita registrarCheckIn(Long idGuarda, Long idVisita);

    /**
     * Registra la salida de la visita (permiso 'registrar_checkout').
     */
    Visita registrarCheckOut(Long idGuarda, Long idVisita);

    /**
     * Busqueda por texto usada por la pantalla del guarda.
     */
    List<Visita> buscarPorTexto(String texto);

    Visita buscarPorId(Long idVisita);

    /**
     * Lista todas las visitas registradas en el sistema.
     */
    List<Visita> listarTodas();

    // =========================================================================
    // [EXAMEN - FUNCION 2: Cancelación de Visitas]
    // Permite a un funcionario o administrador cancelar una visita pendiente/programada
    // antes de que se realice el ingreso en torniquetes.
    // =========================================================================
    Visita cancelar(Long idUsuario, Long idVisita);
}
