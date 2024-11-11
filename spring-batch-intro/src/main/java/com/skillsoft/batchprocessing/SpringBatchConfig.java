package com.skillsoft.batchprocessing;

import com.skillsoft.model.FlightTicket;
import com.skillsoft.service.CustomItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.net.MalformedURLException;

public class SpringBatchConfig {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Value("read/entries.csv")
    private Resource inputCsv;

    @Value("file:write/xml/resultUsingConfig.xml")
    private Resource resultXml;

    @Bean
    public ItemReader<FlightTicket> itemReader() throws UnexpectedInputException, ParseException {

        FlatFileItemReader<FlightTicket> reader = new FlatFileItemReader<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();

        String[] tokens = {"name", "ticketnumber", "route", "ticketprice"};

        tokenizer.setNames(tokens);
        reader.setResource(inputCsv);
        reader.setLinesToSkip(1);

        BeanWrapperFieldSetMapper<FlightTicket> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(FlightTicket.class);

        DefaultLineMapper<FlightTicket> defaultLineMapper = new DefaultLineMapper<>();
        defaultLineMapper.setLineTokenizer(tokenizer);
        defaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

        reader.setLineMapper(defaultLineMapper);

        return reader;
    }

    @Bean
    public ItemProcessor<FlightTicket, FlightTicket> itemProcessor(){
        return new CustomItemProcessor();
    }

    @Bean
    public ItemWriter<FlightTicket> itemWriter(Marshaller marshaller) throws MalformedURLException{

        StaxEventItemWriter<FlightTicket> itemWriter = new StaxEventItemWriter<>();
        itemWriter.setMarshaller(marshaller);
        itemWriter.setRootTagName("ticketRecords");
        itemWriter.setResource((WritableResource) resultXml);

        return itemWriter;
    }

    @Bean
    public Marshaller marshaller(){
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(FlightTicket.class);

        return marshaller;
    }

    @Bean
    protected Step step1 (ItemReader<FlightTicket> reader,
                          ItemProcessor<FlightTicket, FlightTicket> processor,
                          ItemWriter<FlightTicket> writer){

        return steps.get("step1")
                .<FlightTicket, FlightTicket>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean(name = "CSVtoXML")
    public Job job(@Qualifier("step1") Step step1){
        return jobs.get("CSVtoXML").start(step1).build();
    }

}
