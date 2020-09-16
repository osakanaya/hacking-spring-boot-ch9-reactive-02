package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.data.repository.CrudRepository;

import reactor.core.publisher.Mono;

public interface UserRepository extends CrudRepository<User, String>{
	Mono<User> findByName(String name);
}
