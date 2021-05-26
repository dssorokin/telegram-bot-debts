package com.telegram.debt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class DebtBot extends AbilityBot {

    private final static String BOT_TOKEN = "1551656674:AAESrmUK4vR8Pw4Rdq3CPSPGTRL0qlJ8Lro";
    private final static String BOT_USERNAME = "FriendsDebtsBot";
    @Value("${telegram.creator.bots.id}")
    private long CREATOR_ID;

    @Autowired
    private UserManager userManager;

    public DebtBot(UserManager userManager) {
        super(BOT_TOKEN, BOT_USERNAME);
        this.userManager = userManager;
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
              Long user = ctx.user().getId();
              Long groupId = ctx.chatId();
              String userName = ctx.user().getUserName();
              userManager.registerUserInGroup(user, groupId, userName);
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
              String borrowerLink;
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
                  return;
              }

              borrowerLink = commandArguments.get(1);
             if (!userManager.isUserExisted(borrowerLink)) {
                log.error("User with such link: {}", borrowerLink);
                silent.send("Пользователя, которому вы хотяти занять денег, не зарегистрирован.", ctx.chatId());
                return;
             }

             if (commandArguments.get(2).isEmpty()) {
                 log.error("Debt's description is empty for user: {}", ctx.user().getId());
                 silent.send("Укажите пожалуйста описание долга.", ctx.chatId());
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
