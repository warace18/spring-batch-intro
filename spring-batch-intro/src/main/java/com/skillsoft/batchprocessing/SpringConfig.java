package com.skillsoft.batchprocessing;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.net.MalformedURLException;

@Configuration
@EnableBatchProcessing
public class SpringConfig {

    @Value("classpath:org/springframework/batch/core/schema-drop-mysql.sql")
    private Resource dropRepositoryTables;

    @Value("classpath:org/springframework/batch/core/schema-mysql.sql")
    private Resource dataRepositoryTables;

    @Bean
    public DataSource dataSource(){

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/BatchDB?createDatabaseIfNotExist=true" +
                           "&allowPublicKeyRetrieval=true" +
                           "&useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        return  dataSource;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) throws MalformedURLException{

        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();

        databasePopulator.addScript(dropRepositoryTables);
        databasePopulator.addScript(dataRepositoryTables);
        databasePopulator.setIgnoreFailedDrops(true);

        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(databasePopulator);

        return dataSourceInitializer;
    }

    private JobRepository getJobRepository() throws Exception{

        JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setTransactionManager(getTransactionManager());
        factoryBean.afterPropertiesSet();

        return factoryBean.getObject();

    }

    private PlatformTransactionManager getTransactionManager() {
        return new ResourcelessTransactionManager();
    }

    public JobLauncher getJobLauncher() throws Exception{

        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        jobLauncher.afterPropertiesSet();

        return jobLauncher;
    }

}
