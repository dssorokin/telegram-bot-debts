package com.telegram.debt;

import com.telegram.debt.exceptions.DebtException;
import com.telegram.debt.exceptions.NoSuchUserException;
import com.telegram.debt.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DebtBot extends AbilityBot {

    private final static String BOT_TOKEN = "1551656674:AAESrmUK4vR8Pw4Rdq3CPSPGTRL0qlJ8Lro";
    private final static String BOT_USERNAME = "FriendsDebtsBot";
    private final static String COMMON_ERROR = "Что-то пошло не так. Попробуйте ваш запрос позже.";
    private final static Pattern LINK_PATTERN = Pattern.compile("@(\\w+)");

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
          .name("Зарегестрироваться")
          .info("Please use this command to register yourself")
          .locality(Locality.ALL)
          .privacy(Privacy.PUBLIC)
          .action(ctx -> {
              Long user = ctx.user().getId();
              Long groupId = ctx.chatId();
              String userName = ctx.user().getUserName();
              try {
                  userManager.registerUserInGroup(user, groupId, userName);
                  silent.send("Регистрация прошла успешно!", ctx.chatId());
              } catch (Exception e) {
                  log.error("Error occurred in registration process", e);
                  sendCommonError(e, ctx);
              }
          })
          .build();
    }

    public Ability borrowMoney() {
        return Ability.builder()
          .name("Занять")
          .input(3)
          .info("Borrow money")
          .locality(Locality.ALL)
          .privacy(Privacy.PUBLIC)
          .action(ctx -> {
             try {
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
             } catch (DebtException e) {
                 sendCommonError(e, ctx);
             }
          })
          .build();
    }


    public Ability lendMoney() {
        return Ability
          .builder()
          .name("Одолжить")
          .input(3)
          .info("Lend money")
          .locality(Locality.ALL)
          .privacy(Privacy.PUBLIC)
          .action(ctx -> {
              try {
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
                  if (borrowerUser == null) {
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
              } catch (DebtException e) {
                  sendCommonError(e, ctx);
              }
          })
          .build();
    }

    public Ability printDebtsForMe() {
        return Ability.builder()
                .name("мои_долги")
                .input(0)
                .info("Print user's debts")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    try {
                        Map<String, BigDecimal> summaryDebts = debtAccountManager.getDebtsSummaryForUser(ctx.user().getId());
                        if (summaryDebts.size() == 0) {
                            silent.send("Вы никому должны и вам тоже.Успехов", ctx.chatId());
                            return;
                        }
                        final String result = summaryDebts.entrySet().stream().map(DebtBot::printDebtsForUser).collect(Collectors.joining("\n"));
                        silent.send(result, ctx.chatId());
                    } catch (DebtException e) {
                        sendCommonError(e, ctx);
                    }
                })
                .build();
    }

    public Ability payForDebt() {
        return Ability.builder()
                .name("заплатить")
                .input(2)
                .info("Pay for debts")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    BigDecimal payedDebtAmount;

                    List<String> commandArguments = Arrays.asList(ctx.arguments());

                    try {
                        payedDebtAmount = new BigDecimal(commandArguments.get(0));
                    } catch (NumberFormatException e) {
                        log.error("First argument isn't number format. {}", commandArguments.get(0), e);
                        silent.send("Укажите первым сумму долга", ctx.chatId());
                        return;
                    }

                    if (!LINK_PATTERN.asMatchPredicate().test(commandArguments.get(1))) {
                        log.warn("The username {} doesn't match pattern.", commandArguments.get(1));
                        silent.send("Имя пользователя должно начинаться с @", ctx.chatId());
                        return;
                    }

                    try {
                        debtAccountManager.payForDebt(ctx.user().getId(), commandArguments.get(1), payedDebtAmount, ctx.chatId());
                    } catch (NoSuchUserException e) {
                        log.warn("User with such id: {} doesn't exist", e.getUserId(), e);
                        silent.send("Пользователя с именем " + commandArguments.get(1)  +  " не существует", ctx.chatId());
                    } catch (DebtException e) {
                        sendCommonError(e, ctx);
                    }
                })
                .build();
    }

    private static String printDebtsForUser(Map.Entry<String, BigDecimal> userDebtsEntry) {
        if (userDebtsEntry.getValue().compareTo(BigDecimal.ZERO) < 0) {
            return String.format("Вы должны %s пользователю %s", userDebtsEntry.getValue().abs(), userDebtsEntry.getKey());
        } else {
            return String.format("Пользователь %s вам должен %s", userDebtsEntry.getKey(), userDebtsEntry.getValue());
        }
    }

    private void sendCommonError(Exception e, MessageContext ctx) {
        log.error("DebtException occurred for user: {}", ctx.user().getId(), e);
        silent.send(COMMON_ERROR, ctx.chatId());
    }
}
