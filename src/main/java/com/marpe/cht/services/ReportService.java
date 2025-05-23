package com.marpe.cht.services;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.marpe.cht.entities.Colaborador;
import com.marpe.cht.entities.Order;
import com.marpe.cht.entities.Atividade;
import com.marpe.cht.exceptions.InvalidRequestException;
import com.marpe.cht.repositories.ColaboradorRepository;
import com.marpe.cht.repositories.AtividadeRepository;
import com.marpe.cht.repositories.OrderRepository;
import com.marpe.cht.repositories.ReportRepository;

@Service
public class ReportService {

	private static final Logger log = LoggerFactory.getLogger(ReportService.class);
	DateTimeFormatter dtfmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	
	private final AtividadeRepository atividadeRepository;
	private final OrderRepository orderRepository;
	private final ColaboradorRepository colaboradorRepository;
	private final ReportRepository reportRepository;
	
	public ReportService(AtividadeRepository atividadeRepository, OrderRepository orderRepository,
			ColaboradorRepository colaboradorRepository, ReportRepository reportRepository) {
		this.atividadeRepository = atividadeRepository;
		this.orderRepository = orderRepository;
		this.colaboradorRepository = colaboradorRepository;
		this.reportRepository = reportRepository;
	}

	public List<Atividade> AtividadePorPeriodo(String startDate, String endDate, Boolean todosPagos) {
	    
		LocalDate DataInicio = LocalDate.parse(startDate, dtfmt);
		LocalDate DataFim = LocalDate.parse(endDate, dtfmt);
		
		List<Atividade> atividade = atividadeRepository.findAll();
		List<Atividade> filtradas = atividade.stream()
			.filter(x -> x.getOrder().getDataInicio().isEqual(DataInicio) || x.getOrder().getDataInicio().isAfter(DataInicio))
			.filter(x -> x.getOrder().getDataInicio().isEqual(DataFim) || x.getOrder().getDataInicio().isBefore(DataFim))
			.filter(x -> x.getOrder().getConcluida().equals(true))
			.filter(x -> x.getPago().equals(todosPagos))
			.collect(Collectors.toList());
		
		if(filtradas.isEmpty()) {
			throw new InvalidRequestException("Não há atividades no período selecionado.");
		} else {
			return filtradas;
		}
	 }

	
	public List<Object[]> topFiveAtividadeSomadoPorPeriodo(String startDate, String endDate) {
		
		java.sql.Date sqlDataInicio = transformStringStartDateToSqlStartDate(startDate);
		java.sql.Date sqlDataFim = transformStringEndDateToSqlEndDate(endDate);
	
		List<Object[]> result = reportRepository.topFiveAtividadeSomadoPorPeriodo(sqlDataInicio, sqlDataFim);
		
		List<Object[]> completeList = result.stream()
			.map(x -> {
				x[0] = getNomeColaborador(new BigDecimal(x[0].toString()).longValue());
				return new Object[]{x[0], x[1]};
			})
			.collect(Collectors.toList());
		
		return completeList;
	}
	
	public String getNomeColaborador(Long id) {
		List<Colaborador> list = colaboradorRepository.findAll();
		String nome = "";
		for(Colaborador c : list) {
			if(c.getId().equals(id)) {
				nome = c.getDadosPessoais().getNome();
			}
		}
		return nome;
	}
		
	public List<Order> listOrderDesc5() {
		List<Order> list = orderRepository.findAll().stream()
				.sorted((f1, f2) -> Long.compare(f2.getId(), f1.getId()))
				.limit(5)
				.collect(Collectors.toList());
		return list;
	}
	
	public List<Atividade> listAtividadeDesc5() {
		List<Atividade> list = atividadeRepository.findAll().stream()
				.filter(x -> x.getHoraFinal() != null)
				.sorted((f1, f2) -> Long.compare(f2.getId(), f1.getId()))
				.limit(5)
				.collect(Collectors.toList());
		return list;
	}
	
	public List<Colaborador> listColaboradorDesc5() {
		List<Colaborador> list = colaboradorRepository.findAll().stream()
				.sorted((f1, f2) -> Long.compare(f2.getId(), f1.getId()))
				.limit(5)
				.collect(Collectors.toList());
		return list;
	}
	
	private java.sql.Date transformStringStartDateToSqlStartDate(String startDate) {
		
		java.sql.Date sqlDataInicio;
		if(startDate == null) {
			java.util.Date dataInicio = new Date(0);
			dataInicio = Date.from(Instant.now().minus(15, ChronoUnit.DAYS));
			sqlDataInicio = new java.sql.Date(dataInicio.getTime());
		} else {
			java.util.Date dataInicio = new Date(0);
			try {
				dataInicio = new SimpleDateFormat("dd-MM-yyyy").parse(startDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			sqlDataInicio = new java.sql.Date(dataInicio.getTime());
		}
		return sqlDataInicio;
	}
	
	private java.sql.Date transformStringEndDateToSqlEndDate(String endDate) {
		
		java.sql.Date sqlDataFim;
		if(endDate == null) {
			java.util.Date dataFim = new Date(0);
			dataFim = Date.from(Instant.now());
			sqlDataFim = new java.sql.Date(dataFim.getTime());
		} else {
			java.util.Date dataFim = new Date(0);
			try {
				dataFim = new SimpleDateFormat("dd-MM-yyyy").parse(endDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			sqlDataFim = new java.sql.Date(dataFim.getTime());
		}
		return sqlDataFim;
	}
	
}
