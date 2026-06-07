package com.scm.common.utils.security;

/**
 * 命令行：生成 ENC(...) 配置密文
 *
 * <pre>
 * mvn -q -pl scm-common exec:java -Dexec.mainClass=com.scm.common.utils.security.ConfigCryptoTool \
 *   -Dexec.args="encrypt 你的密钥 ScmLocalDevCipherKey2025!"
 * </pre>
 */
public final class ConfigCryptoTool
{
    private ConfigCryptoTool()
    {
    }

    public static void main(String[] args)
    {
        if (args == null || args.length < 2 || !"encrypt".equalsIgnoreCase(args[0]))
        {
            printUsage();
            return;
        }
        String plainText = args[1];
        String cipherKey = args.length >= 3 ? args[2] : System.getenv("SCM_CONFIG_CIPHER_KEY");
        if (cipherKey == null || cipherKey.isEmpty())
        {
            System.err.println("请传入 cipherKey 参数，或设置环境变量 SCM_CONFIG_CIPHER_KEY");
            System.exit(1);
        }
        System.out.println(ConfigCryptoUtils.encrypt(plainText, cipherKey));
    }

    private static void printUsage()
    {
        System.out.println("用法: encrypt <明文> [cipherKey]");
        System.out.println("示例: encrypt AKIDxxxx ScmLocalDevCipherKey2025!");
    }
}
