package jpabook.jpashop.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.Query;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

	private final EntityManager em;

	public void save(Order order) {
		em.persist(order);
	}

	public Order findOne(Long id) {
		return em.find(Order.class, id);
	}

	//// querydsl
	public List<Order> findAll(OrderSearch orderSearch) {
		JPAQueryFactory query = new JPAQueryFactory(em);
		QOrder order = QOrder.order;
		QMember member = QMember.member;
		return query
				.select(order)
				.from(order)
				.join(order.member, member)
				.where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName())).limit(1000)
				.fetch();
	}

	private BooleanExpression statusEq(OrderStatus statusCond) {
		if (statusCond == null) {
			return null;
		}
		return QOrder.order.status.eq(statusCond);
	}

	private BooleanExpression nameLike(String nameCond) {
		if (!StringUtils.hasText(nameCond)) {
			return null;
		}
		return QMember.member.name.like(nameCond);
	}

	////
	public List<Order> findAllByString(OrderSearch orderSearch) {
		// language=JPAQL
		String jpql = "select o From Order o join o.member m";
		boolean isFirstCondition = true;
		// 주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			jpql += " o.status = :status";
		}
		// 회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			jpql += " m.name like :name";
		}
		TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000); // 최대 1000건
		if (orderSearch.getOrderStatus() != null) {
			query = query.setParameter("status", orderSearch.getOrderStatus());
		}
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			query = query.setParameter("name", orderSearch.getMemberName());
		}
		return query.getResultList();
	}

	public List<Order> findAllWithMemberDelivery() {
		return em.createQuery("select o from Order o" + " join fetch o.member m" + " join fetch o.delivery d",
				Order.class).getResultList();
	}

	public List<OrderSimpleQueryDto> findOrderDtos() {
		return em.createQuery("select o from o" + "join o.member m" + "join o.delivery d", OrderSimpleQueryDto.class)
				.getResultList();

	}

	public List<Order> findAllWithItem() {
		return em.createQuery("select distinct o from Order o" + " join fetch o.member m" + " join fetch o.delivery d"
				+ " join fetch o.orderItems oi" + " join fetch oi.item i", Order.class).getResultList();
	}

	public List<Order> findAllWithMemberDelivery(int offset, int limit) {
		return em.createQuery("select o from Order o" + " join fetch o.member m" + " join fetch o.delivery d",
				Order.class).setFirstResult(offset).setMaxResults(limit).getResultList();
	}
}