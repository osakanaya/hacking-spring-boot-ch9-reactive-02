/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.web.bind.annotation.DeleteMapping;
import reactor.core.publisher.Mono;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;

@Controller
public class HomeController {

	private final InventoryService inventoryService;


	private static String cartName(OAuth2User oauth2User) {
		return oauth2User.getName() + "'s Cart";
	}
	
	public HomeController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@GetMapping
	Mono<Rendering> home(
		@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
		@AuthenticationPrincipal OAuth2User oauth2User) {
		return Mono.just(Rendering.view("home.html") // <2>
			.modelAttribute("items", this.inventoryService.getInventory()) // <3>
			.modelAttribute("cart", this.inventoryService.getCart(cartName(oauth2User)) // <4>
				.defaultIfEmpty(new Cart(cartName(oauth2User))))
			.modelAttribute("userName", oauth2User.getName())
			.modelAttribute("authorities", oauth2User.getAuthorities())
			.modelAttribute("clientName", authorizedClient.getClientRegistration().getClientName())
			.modelAttribute("userAttributes", oauth2User.getAttributes())
			.build());
	}

	@PostMapping("/cart/item/add/{id}")
	Mono<String> addToCart(@AuthenticationPrincipal OAuth2User oauth2User, @PathVariable String id) {
		return this.inventoryService.addItemToCart(cartName(oauth2User), id)
			.thenReturn("redirect:/");
	}

	@DeleteMapping("/cart/item/remove/{id}")
	Mono<String> removeFromCart(@AuthenticationPrincipal OAuth2User oauth2User, @PathVariable String id) {
		return this.inventoryService.removeOneFromCart(cartName(oauth2User), id)
			.thenReturn("redirect:/");
	}

	@PostMapping("/item")
	Mono<String> createItem(@ModelAttribute Item newItem) {
		return this.inventoryService.saveItem(newItem) //
			.thenReturn("redirect:/");
	}

	@DeleteMapping("/item/{id}")
	Mono<String> deleteItem(@PathVariable String id) {
		return this.inventoryService.deleteItem(id) //
			.thenReturn("redirect:/");
	}
}
