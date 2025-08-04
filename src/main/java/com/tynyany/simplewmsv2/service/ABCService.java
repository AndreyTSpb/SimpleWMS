package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.entity.ABC;

import java.util.List;

public interface ABCService {
    ABC getByID(int abcID);
    List<ABC> getAll();
    int addABC(ABC abc);
    void updateABC(ABC abc);
    void delABC(ABC abc);
}
