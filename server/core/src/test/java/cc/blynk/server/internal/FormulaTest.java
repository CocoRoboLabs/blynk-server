package cc.blynk.server.internal;

import cc.blynk.server.exp4j.Expression;
import cc.blynk.server.exp4j.ExpressionBuilder;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class FormulaTest {

    @Test
    public void testFormula() {
        Expression e = new ExpressionBuilder("x - y")
                .variables("x", "y")
                .build()
                .setVariable("x", 2)
                .setVariable("y", 3);
        assertEquals(-1, e.evaluate(), 0.01);
    }

    @Test
    public void testFormulaRule() {
        /*
        RuleGroup rules = new RuleGroup();

        int product1 = 1;
        short product1SourcePin = 1;
        short product1TargetPin = 2;

        int product2 = 2;
        short product2SourcePin = 1;

        DeviceRuleDataStream triggerDataStream = new DeviceRuleDataStream(product1, product1SourcePin, PinType.VIRTUAL);
        ProductDataStreamTrigger trigger = new ProductDataStreamTrigger(triggerDataStream);

        NumberUpdatedCondition numberUpdatedCondition = new NumberUpdatedCondition();

        DeviceRuleDataStream setValueDataStream = new DeviceRuleDataStream(product1, product1TargetPin, PinType.VIRTUAL);
        DeviceRuleDataStream sourceDataStream = new DeviceRuleDataStream(product2, product2SourcePin, PinType.VIRTUAL);
        FormulaValue formulaValue = new FormulaValue(
                "x - y",
                Map.of("x", new TriggerDataStreamFormulaParam(),
                       "y", new BackDeviceReferenceFormulaParam(sourceDataStream))
        );
        SetDataStreamAction setNumberPinAction = new SetDataStreamAction(setValueDataStream, formulaValue);

        Rule rule = new Rule(trigger, numberUpdatedCondition, setNumberPinAction);

        rules.rules = new Rule[] {
                rule
        };

        Device device = new Device();
        device.productId = product1;
        rules.process(device, (short) 1, PinType.VIRTUAL, "123");
        */
    }

}
