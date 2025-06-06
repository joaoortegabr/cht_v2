package com.marpe.cht.entities.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import com.marpe.cht.entities.Cliente;
import com.marpe.cht.entities.Colaborador;
import com.marpe.cht.entities.Regional;

public record OrderRequest(
		Cliente cliente,
		Regional regional,
		Colaborador colaborador,
		LocalDate dataInicio,
		LocalTime horaInicio,
		String observacao,
		Boolean todosPagos,
		Boolean concluida
		) {

}
