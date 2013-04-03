package bg.bozho.quickfix;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class MethodBodyReplacementAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        try {
            InputStream in = MethodBodyReplacementAgent.class.getClassLoader().getResourceAsStream("method-replacements.txt");
            if (in == null) {
                throw new Exception("/method-replacements.txt is missing. Include it and list all classes that define method replacements");
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line = null;
            List<Class<?>> loadedClasses = new ArrayList<Class<?>>();
            while ((line = r.readLine()) != null) {
                loadedClasses.add(Class.forName(line));
            }
            r.close();
            for (Class<?> c : loadedClasses) {
                MethodBodyTransformer.setupMethodRedefinition(c);
            }

            Class<?>[] classes = new Class<?>[MethodBodyTransformer.replacementMethods.size()];
            int i = 0;
            for (String c : MethodBodyTransformer.replacementMethods.keySet()) {
                classes[i++] = Class.forName(c);
            }

            instrumentation.addTransformer(new MethodBodyTransformer(), true);
            instrumentation.retransformClasses(classes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
