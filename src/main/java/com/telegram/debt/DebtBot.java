package com.telegram.debt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class DebtBot extends AbilityBot {

	private final static String BOT_TOKEN = "1551656674:AAESrmUK4vR8Pw4Rdq3CPSPGTRL0qlJ8Lro";
	private final static String BOT_USERNAME = "FriendsDebtsBot";
	@Value("${telegram.creator.bots.id}")
	private long CREATOR_ID;

	private DebtAccountManager debtAccountManager;

	public DebtBot() {
		super(BOT_TOKEN, BOT_USERNAME);
	}

	@Override
	public long creatorId() {
		return CREATOR_ID;
	}


	public Ability registerMe() {
		return Ability
				.builder()
				.name("register_me")
				.info("Please use this command to register yourself")
				.locality(Locality.ALL)
				.privacy(Privacy.PUBLIC)
				.action(ctx -> {

				})
				.build();
	}


	public Ability addDebt() {
		return Ability
				.builder()
				.name("add_debt")
				.input(3)
				.info("Please add your debt")
				.locality(Locality.ALL)
				.privacy(Privacy.PUBLIC)
				.action(ctx -> {
					BigDecimal debtSum;
					String debtor;
					String debtDescription;

					List<String> commandArguments = Arrays.asList(ctx.arguments());
					if (commandArguments.isEmpty()) {
						log.error("Empty argument list");
						silent.send("Укажи информацию о долге", ctx.chatId());
						return;
					}

					try {
						debtSum = new BigDecimal(commandArguments.get(0));
					} catch (NumberFormatException e) {
						log.error("First argument isn't number format. {}", commandArguments.get(0), e);
						silent.send("Укажите первым сумму долга", ctx.chatId());
					}




					silent.send("Hello world!", ctx.chatId());
				})
				.build();
	}

	public Ability sayHelloWorld() {
		return Ability
				.builder()
				.name("hello")
				.input(1)
				.info("says hello world!")
				.locality(Locality.ALL)
				.privacy(Privacy.PUBLIC)
				.action(ctx -> {
					silent.send("Hello world!", ctx.chatId());
				})
				.build();
	}
}
