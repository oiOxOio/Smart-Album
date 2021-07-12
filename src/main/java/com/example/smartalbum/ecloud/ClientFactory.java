package com.example.smartalbum.ecloud;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.chinamobile.cmss.sdk.ECloudDefaultClient;
import com.chinamobile.cmss.sdk.IECloudClient;
import com.chinamobile.cmss.sdk.http.constant.Region;
import com.chinamobile.cmss.sdk.http.signature.Credential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 生成client实例
 *
 * @author Administrator
 */
@Component
public class ClientFactory {

    @Value("${ecloud.ossAccessKey}")
    private String ossAccessKey;
    @Value("${ecloud.ossSecretKey}")
    private String ossSecretKey;
    @Value("${ecloud.ossEndPoint}")
    private String ossEndPoint;

    @Value("${ecloud.ecloudAccessKey}")
    private String ecloudAccessKey;
    @Value("${ecloud.ecloudSecretKey}")
    private String ecloudSecretKey;

    @Bean
    public AmazonS3Client createS3Client() {
        //s3 oss Client
        AWSCredentials credentials = new BasicAWSCredentials(ossAccessKey, ossSecretKey);

        ClientConfiguration opts = new ClientConfiguration();
        opts.setSignerOverride("S3SignerType");

        S3ClientOptions options = new S3ClientOptions();
        options.setPathStyleAccess(true);

        AmazonS3Client client = new AmazonS3Client(credentials, opts);
        client.setS3ClientOptions(options);
        client.setEndpoint(ossEndPoint);

        return client;
    }

    @Bean
    public IECloudClient createCloudClient() {
        //移动云api client
        Credential credential = new Credential(ecloudAccessKey, ecloudSecretKey);
        return new ECloudDefaultClient(credential, Region.POOL_SZ);
    }
}
