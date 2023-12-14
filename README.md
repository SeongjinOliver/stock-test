# stock-test


# 자바 synchronized의 문제점
- 자바의 synchronized는 하나의 프로세스 안에서만 보장이 된다. 서버가 1대일 때는 데이터의 접근을 서버가 1대만 해서 괜찮겠지만, 서버가 2대 혹은 그 이상일 경우는 데이터의 접근을 여러 대에서 할 수 있게 된다.

# Mysql을 활용한 다양한 방법

1. Pessimistic Lock
   - 실제로 데이터에 Lock을 걸어서 정합성을 맞추는 방법이다. exclusive lock을 걸게되면 다른 트랜잭션에서는 lock이 해제되기 전에 데이터를 가져갈 수 없게된다. 데드락이 걸릴 수 있기 때문에 주의하여 사용하여야 한다.
   - 예를들어, 서버가 여러대 있을 때 서버1이 락을 걸고 데이터를 가져가게 되면 다른 서버는 서버1이 락을 해제하기 전까지는 데이터를 가져갈 수 없게 된다.
   - 장점: 충돌이 빈번하게 일어 난다면 Optimistic Lock보다 성능이 좋을 수 있다.
   - 장점2: 별도의 락을 잡기 때문에 데이터 정합성이 보장된다.
   - 단점: 별도의 락을 잡기 때문에 성능 감소가 있을 수 있다.
2. Optimistic Lock
    - 실제로 Lock을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법이다. 먼저 데이터를 읽은 후에 update를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하여 업데이트 한다. 내가 읽은 버전에서 수정사항이 생겼을 경우에는 application에서 다시 읽은 후에 작업을 수행해야 한다.
    - ![image](https://github.com/SeongjinOliver/stock-test/assets/55625864/40c66e9a-7860-4ae3-bdf8-c7f578e5f767)
    - 서버1과 서버2가 데이터베이스에서 버전이 1인 것을 읽어 왔고, 읽고 난 이후에 서버1이 먼저 업데이트 쿼리를 날린다. 업데이트 쿼리를 수행할 때 where 조건에 where 버전이 1인걸 명시 해주면 된다. update할 때 version을 1 증가 시켜서 업데이트 후 실제 데이터는 버전이 2가 된다. 그리고 서버 2가 이후에 동일하게 업데이트 쿼리를 수행한다. 마찬가지로 현재 읽은 버전을 업데이트를 하는 조건이 포함되어 있기 때문에 업데이트는 수행되지 않는다. 왜냐하면 현재 읽은 버전은 버전이 1이고 실제데이터를 버전이 2인 상태이기 때문이다. 업데이트가 실패하게 되면서 실제 어플리케이션에서 다시 읽은 후에 작업을 수행 해야 하는 로직을 넣어 줘야 한다.
    - 장점: 별도의 락을 잡지 않으므로 Pessimistic Lock보다 성능상 이점이 있다.
    - 단점: 업데이트가 실패했을 때 재시도 로직을 개발자가 직접 작성해 주어야 하는 번거로움이 존재한다.
3. Named Lock
    - 이름을 가진 metadata locking입니다. 이름을 가진 lock을 획득한 후 해제할 때까지 다른 세션을 이 lock을 획득할 수 없도록 합니다.
    - Passimistic Lock과 유사한데 차이점은 Passimistic 락은 로우나 테이블 단위로 걸지만 Named Lock은 메타데이터의 locking을 하는 방법이다.
    - 주의할 점으로는 트랜잭션이 종료될 때 락이 자동으로 해제되지 않기 때문에 별도의 명령어로 해제를 수행해 주거나 선점 시간이 끝나야 락이 해제된다.
    - MySQL에서는 get-lock 명령어를 통해 named-lock을 획득할 수 있고 release-lock 명령어를 통해 lock을 해제할 수 있다. 
    - Named Lock은 주로 분산락을 구현할 때 사용한다. 
    - Pessimistic Lock은 타임아웃을 구현하기 힘들지만 Named Lock은 타임아웃을 손쉽게 구현할 수 있다. 그 외에도 데이터 삽입 시에 정합성을 맞춰야 하는 경우에도 Named Lock을 사용할 수 있다. 
    - 하지만 이 방법은 트랙잭션 종료 시에 락 해제, 세션 관리를 잘 해줘야 되기 때문에 주위해서 사용해야 하고 실제로 사용할 때는 구현 방법이 복잡할 수 있다.

- 충돌이 빈번하게 일어난다면 혹은 충돌이 번번하게 일어날 것이라고 예상된다면 Pessimistic Lock을추천하고 빈번하게 일어나지 않을 것이라고 예상된다면 Optimistic Lock을 추천한다.

# Redis를 활용한 동시성 문제 해결
### Lettuce
- SETNX 명령어(SET if Not eXists)를 활용하여 분산락 구현
  - 키와 밸류를 set할 때 기존의 값이 없을 때만 set하는 명령어
  - SETNX를 활용하는 방식은 SpinLock 방식이므로 retry로직을 개발자가 작성
  - SpinLock이란 락을 획득하려는 스레드가 락을 사용할 수 있는지 반복적으로 확인하면서 락 획득을 시도하는 방식
  - 장점: 구현이 간단함
  - 단점: SpinLock 방식이므로 레디스에 부화를 줄 수 있음
- spin lock 방식

### Redisson
- pub-sub 기반으로 Lock 구현 제공
- retry 로직을 작성하지 않아도 됨

# Docker Redis lock 환경 셋팅 및 확인 명령어
```shell
  docker pull redis
  docker run --name myredis -d -p 6379:6379 redis
  docker ps
  docker exec -it [CONTAINER ID] redis-cli
# redis-cli 진입
# key: 1, value: lock
  setnx 1 lock
  setnx 1 lock # 1인 key가 존재해서 실패 
  del 1 # 삭제하고
  setnx 1 lock # 성공
```

- Redis를 활용한 방법은 MySQL의 Named Lock과 거의 비슷하다고 생각하면 됨
- 다른점은 Redis를 이용한다는 점과 Session 관리에 신경을 안 써도 됨

## Redisson 실습 명령어
- 2개의 터미널에서 실습 진행
```shell
# 1
docker exec -it [CONTAINER_ID] redis-cli
subscribe ch1
# 2
docker exec -it [CONTAINER_ID] redis-cli
publish ch1 hello
```

- Redisson은 자신이 점유하고 있는 락을 해제할 때 채널에 메시지를 내줌으로써 락을 획득해야 하는 스레드들에게 락 획득을 하라고 전달함
- 그러면 락 획득을 해야 하는 스레드들은 메시지를 받았을 때 락 획득을 시도하게 됨
- Lettuce는 계속 락 획득을 시도하는 반면에 Redisson은 락 해제가 되었을 때 한번 혹은 몇번만 시도를 하기 때문에 Redis의 부하를 줄여줌
- Redisson 같은 경우에는 락 관련된 클래스들을 라이브러리에서 제공을 해주므로 우리가 별도의 레포지토리를 작성하지 않아도 됨
- Redisson을 활용한 방법은 PubSub 기반의 구현이기 때문에 Redis의 부하를 중여준다는 장점이 있습니다. 하지만 구현이 조금 복잡하다는 단점과 별도의 라이브러리를 사용해야 하는 부담감이 있음

# Lettuce 장단점
- 구현이 간단하다.
- spring data redis를 이용하면 lettuce가 기본이기 때문에 별도의 라이브러리를 사용하지 않아도 됨
- spin lock 방식이기 때문에 동시에 많은 스레드가 lock 획득 대기 상태라면 redis에 부하가 갈 수 있음

# Redisson 장단점
- lock 획득 재시도를 기본으로 제공
- pub-sub 방식으로 구현이 되어있기 때문에 lettuce 와 비교했을 때 redis에 부하가 덜 감
- lock을 라이브러리 차원에서 제공해 주기 때문에 사용법을 공부해야 함

# 실무에서는 ?
- 재시도가 필요하지 않은 lock은 lettuce 활용
- 재시도가 필요한 경우에는 redisson을 활용

# Mysql 장단점
- 이미 MySQL을 사용하고 있다면 별도의 비용 없이 사용 가능함
- 어느정도의 트래픽까지는 문제 없이 활용이 가능
- Redis 보다는 성능이 좋지 않음

# Redis 장단점
- 활용중인 Redis 가 없다면 별도의 구축 비용과 인프라 관리 비용이 발생함
- MySQL 보다 성능이 좋음