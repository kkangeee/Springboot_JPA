package jpabook.jpashop.domain;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter @Setter
public class Delivery {
	 @Id @GeneratedValue
	 @Column(name = "delivery_id")
	 private Long id;
	 //@JsonIgnore
	 @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
	 private Order order;
	 
	 @Embedded
	 private Address address;
	 
	 @Enumerated(EnumType.STRING)
	 private DeliveryStatus status; //ENUM [READY(준비), COMP(배송)]
}