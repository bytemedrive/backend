package com.bytemedrive.backend.command.file.entity;

import com.bytemedrive.backend.store.entity.AbstractAggregate;
import com.bytemedrive.backend.store.entity.RootAggregate;


@RootAggregate(FileAggregate.NAME)
public class FileAggregate extends AbstractAggregate {

    public static final String NAME = "file";

    public String id;


    @Override
    public String getAggregateId() {
        return id;
    }
}
