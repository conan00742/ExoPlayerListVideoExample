package com.example.krot.videoplayermanager.event_bus;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by Krot on 1/29/18.
 */

public class RxBus {
    public final Subject<Object> _bus = PublishSubject.create().toSerialized();

    public void send(Object o) { _bus.onNext(o);}

    public Observable<Object> toObserverable() {return _bus;}
}
