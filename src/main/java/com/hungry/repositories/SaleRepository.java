package com.hungry.repositories;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hungry.entities.Product;

@Repository
public interface SaleRepository extends JpaRepository<Product, Integer> {

	@Transactional
	@Modifying
	@Query(value = "UPDATE hungry_products SET hungry_products.total_sold_price= :new_sold_price, hungry_products.total_sold_peices= :new_sold_peices WHERE hungry_products.product_id= :product_id", nativeQuery = true)
	public void update(@Param("new_sold_price") double newSoldPrice, @Param("new_sold_peices") int newSoldPeices,
			@Param("product_id") int productId);

	@Query(value = "SELECT price FROM hungry_products WHERE product_id=:product_id", nativeQuery = true)
	public double findProductPrice(@Param("product_id") int productId);

	@Query(value = "SELECT total_sold_price FROM hungry_products WHERE product_id=:product_id", nativeQuery = true)
	public double findProductSoldPrice(@Param("product_id") int productId);

	@Query(value = "SELECT total_sold_peices FROM hungry_products WHERE product_id=:product_id", nativeQuery = true)
	public int findProductSoldPeices(@Param("product_id") int productId);

	@Query(value = "SELECT buyers FROM hungry_products WHERE product_id=:product_id", nativeQuery = true)
	public String findProductBuyers(@Param("product_id") int productId);

}