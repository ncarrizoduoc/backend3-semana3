package com.duoc.banco.item;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.duoc.banco.model.Interes;

@Configuration
public class InteresItemReaderConfig {

    @Bean(name = "interesItemReader")
    @StepScope
    public FlatFileItemReader<Interes> interesItemReader(
        @Value("${app.input-intereses}") Resource inputFile,
        @Value("#{stepExecutionContext['start']}") int start, // Inyecta el límite inferior de la partición
        @Value("#{stepExecutionContext['end']}") int end // Inyecta el límite superior de la partición
    ) {
        FlatFileItemReader<Interes> reader =  new FlatFileItemReaderBuilder<Interes>()
            .name("interesItemReader")
            .resource(inputFile)
            .encoding("UTF-8")
            .linesToSkip(1)
            .delimited()
            .delimiter(",")
            .names("cuentaId", "nombre", "saldo", "edad", "tipo")
            .fieldSetMapper(interesFieldSetMapper())
            .build();
        
        reader.setCurrentItemCount(start); // Límite inferior de la partición 
        reader.setMaxItemCount(end + 1); // Límite superior de la partición (se suma 1 porque maxItemCount es exclusivo)

        return reader;
    }

    private FieldSetMapper<Interes> interesFieldSetMapper() {
        return fieldSet -> {
            Interes interes = new Interes();
            interes.setCuentaId(fieldSet.readLong("cuentaId"));
            interes.setNombre(fieldSet.readString("nombre"));
            interes.setSaldoInicial(fieldSet.readInt("saldo"));
            interes.setEdad(fieldSet.readInt("edad"));
            interes.setTipo(fieldSet.readString("tipo"));
            return interes;
        };
    }

}
