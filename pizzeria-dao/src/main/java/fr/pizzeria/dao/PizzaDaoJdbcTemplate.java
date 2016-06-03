package fr.pizzeria.dao;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.pizzeria.exception.DaoException;
import fr.pizzeria.model.CategoriePizza;
import fr.pizzeria.model.Pizza;

@Repository
@Lazy
public class PizzaDaoJdbcTemplate implements IPizzaDao {

	private JdbcTemplate jdbcTemplate;
	private RowMapper<Pizza> rowMapperPizza = (rs, rowNum) -> new Pizza(rs.getString("reference"), rs.getString("libelle"),
			rs.getDouble("prix"), CategoriePizza.valueOf(rs.getString("categorie")));

	@Autowired
	private BatchPizzaDaoJDBCTemplate batchPizzaDaoJDBCTemplate;

	@Autowired
	public PizzaDaoJdbcTemplate(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Pizza> findAllPizzas() throws DaoException {
		String sql = "SELECT * FROM pizza";
		return jdbcTemplate.query(sql, rowMapperPizza);
	}

	@Override
	public Pizza findByCode(String code) throws DaoException {
		String sql = "SELECT * FROM pizza WHERE reference = ?";
		
		try {
			return this.jdbcTemplate.queryForObject(sql, rowMapperPizza, code);
        } catch (DataAccessException e) {
            return null;
        }
	}

	@Override
	public boolean savePizza(Pizza newPizza) throws DaoException {
		String sql = "INSERT INTO pizza(reference, libelle, prix, categorie) " + "VALUES(?, ?, ?, ?)";
		jdbcTemplate.update(sql, newPizza.getCode(), newPizza.getNom(), newPizza.getPrix(),
				newPizza.getCategorie().name());

		return true;
	}

	@Override
	public boolean updatePizza(String codePizza, Pizza updatePizza) throws DaoException {
		String sql = "UPDATE pizza " + "SET libelle = ?, prix = ?, categorie = ? " + "WHERE reference = ?";
		jdbcTemplate.update(sql, updatePizza.getNom(), updatePizza.getPrix(), updatePizza.getCategorie().name(),
				updatePizza.getCode());

		return true;
	}

	@Override
	public boolean deletePizza(String codePizza) throws DaoException {
		String sql = "DELETE FROM pizza " + "WHERE reference = ?";
		jdbcTemplate.update(sql, codePizza);

		return true;
	}

	@Override
	@Transactional
	public boolean saveAllPizzas(List<Pizza> listPizzas, int nb) throws DaoException {
		List<List<Pizza>> listPartitionnee = ListUtils.partition(listPizzas, nb);

		for (List<Pizza> list3Pizzas : listPartitionnee) {
			batchPizzaDaoJDBCTemplate.insertListPizzas(list3Pizzas);
		}

		return true;
	}

	// @Override
	// public boolean saveAllPizzas(List<Pizza> listPizzas, int nb) throws
	// DaoException {
	// List<List<Pizza>> listPartitionnee = ListUtils.partition(listPizzas, nb);
	//
	// for (List<Pizza> list3Pizzas : listPartitionnee) {
	// transactionTemplate.execute(status -> {
	//
	// for (Pizza pizza : list3Pizzas) {
	// if (pizza.getCode().equals("ORI")) {
	// status.setRollbackOnly();
	// break;
	// }
	// String sql = "INSERT INTO pizza(reference, libelle, prix, categorie) "
	// + "VALUES(?, ?, ?, ?)";
	// jdbcTemplate.update(sql, pizza.getCode(), pizza.getNom(),
	// pizza.getPrix(), pizza.getCategorie().name());
	// }
	//
	// return null;
	// });
	// }
	//
	// return true;
	// }
}