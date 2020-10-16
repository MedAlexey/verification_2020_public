import CFG.CFGToDotConverter;
import org.junit.jupiter.api.Test;

public class Tests extends BaseTest {

    @Test
    public void tmpTest() throws Exception{
        final String testFilePath = "src/test/java/resources/Tmp.java";
        final String outputImgPath = "src/test/java/out/Tmp.png";

        new CFGToDotConverter()
                .convert(buildCFG(testFilePath))
                .saveGraph(outputImgPath);
    }
}
