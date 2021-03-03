package org.oneworldaccuracy.demo.validator;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RepeatCharacterRegexRule;
import org.passay.Rule;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.join;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
    public static CharacterRule[] getRules() {
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(1);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(1);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(1);

        CharacterData specialChars = new SpecialCharacterData();
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(1);
        return new CharacterRule[]{lowerCaseRule, upperCaseRule, digitRule, splCharRule};
    }

    @Override
    public boolean isValid(String value, final ConstraintValidatorContext context) {
        String password = value;
        if (password == null) {
            password = "";
        }

        List<Rule> rules = new ArrayList<>();
        rules.addAll(Arrays.asList(getRules()));
        rules.addAll(Arrays.asList(
            new LengthRule(8, 30),
            new WhitespaceRule(),
            new RepeatCharacterRegexRule(3)
        ));

        final PasswordValidator validator = new PasswordValidator(rules);
        final RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(join(",", validator.getMessages(result))).addConstraintViolation();
        return false;
    }
}
