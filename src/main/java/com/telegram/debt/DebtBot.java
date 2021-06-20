package com.telegram.debt;

import com.telegram.debt.model.User;
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

    @Autowired
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
              Long user = ctx.user().getId();
              Long groupId = ctx.chatId();
              String userName = ctx.user().getUserName();
              userManager.registerUserInGroup(user, groupId, userName);
          })
          .build();
    }

    public Ability borrowMoney() {
        return Ability.builder()
          .name("borrow")
          .input(3)
          .info("Borrow money")
          .locality(Locality.ALL)
          .privacy(Privacy.PUBLIC)
          .action(ctx -> {
              BigDecimal debtSum;
              String lenderLink;
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

              lenderLink = commandArguments.get(1);
              User lenderUser = userManager.findUserByLink(lenderLink);
              if (lenderUser == null) {
                  log.error("User with such link: {}", lenderLink);
                  silent.send("Пользователь, у которого вы хотите занять денег, не зарегистрирован.", ctx.chatId());
                  return;
              }

              if (commandArguments.get(2).isEmpty()) {
                  log.error("Debt's description is empty for user: {}", ctx.user().getId());
                  silent.send("Укажите пожалуйста описание долга.", ctx.chatId());
                  return;
              }

              debtDescription = commandArguments.get(2);

              try {
                  debtAccountManager.addDebtForGroup(debtSum, lenderUser.getUserId(), ctx.user().getId(), ctx.chatId(), debtDescription);
              } catch (NoSuchUserException e) {
                  log.error("User with such id: {} doesn't exist", e.getUserId(), e);
                  silent.send("Сначала зерегистрируйтесь использовав команду /register_me", ctx.chatId());
                  return;
              }
              silent.send("Ваш долг добавлен", ctx.chatId());
          })
          .build();
    }


    public Ability lendMoney() {
        return Ability
          .builder()
          .name("lend")
          .input(3)
          .info("Lend money")
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
              User borrowerUser = userManager.findUserByLink(borrowerLink);
             if (borrowerLink != null) {
                log.error("User with such link: {}", borrowerLink);
                silent.send("Пользователя, которому вы хотяти занять денег, не зарегистрирован.", ctx.chatId());
                return;
             }

             if (commandArguments.get(2).isEmpty()) {
                 log.error("Debt's description is empty for user: {}", ctx.user().getId());
                 silent.send("Укажите пожалуйста описание долга.", ctx.chatId());
                 return;
             }

             debtDescription = commandArguments.get(2);

             try {
                 debtAccountManager.addDebtForGroup(debtSum, ctx.user().getId(), borrowerUser.getUserId(), ctx.chatId(), debtDescription);
             } catch (NoSuchUserException e) {
                 log.error("No such user with id: {}", e.getUserId(), e);
                 silent.send("Сначала зерегистрируйтесь использовав команду /register_me", ctx.chatId());
                 return;
             }

             silent.send("Ваш долг добавлен", ctx.chatId());
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
