package com.marpe.cht.entities.dtos;

import com.marpe.cht.entities.User;

public record DadosPessoaisRequest(
		User user,
		String nome,
		String rg,
		String cpf,
		String telefone,
		String email,
		String cidade
		) {

}
