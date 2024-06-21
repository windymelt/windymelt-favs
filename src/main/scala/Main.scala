import cats.effect._
import cats.effect.std.Dispatcher
import natchez.Trace.Implicits.noop
import org.atnos.eff._
import org.atnos.eff.addon.cats.effect.IOEffect._
import org.atnos.eff.syntax.addon.cats.effect._
import org.atnos.eff.syntax.all._
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.derivation.default._
import skunk._
import skunk.codec.all._
import skunk.implicits._

import all._

case class Config(
    host: String,
    port: Int,
    user: String,
    database: String,
    password: String,
) derives ConfigReader

type ConfigEither     = [A] =>> Either[ConfigReaderFailures, A]
type _configEither[R] = ConfigEither |= R

object Main extends IOApp {
  val session: Config => Resource[IO, Session[IO]] = (conf: Config) =>
    Session.single(
      host = conf.host,
      port = conf.port,
      user = conf.user,
      database = conf.database,
      password = Some(conf.password),
    )

  def prog[R: _io: _configEither] = for {
    conf <- loadConfig
    _    <- generate(conf)
  } yield ()

  def loadConfig[R: _io: _configEither]: Eff[R, Config] = for {
    confEither <- fromIO(IO(ConfigSource.defaultApplication.load[Config]))
    conf       <- fromEither(confEither)
  } yield conf

  def generate[R: _io](conf: Config): Eff[R, Unit] = fromIO(
    session(conf).use { s =>
      for {
        d <- s.unique(sql"select current_date".query(date)) // (4)
        _ <- IO.println(s"The current date is $d.")
      } yield ()
    },
  )

  def run(args: List[String]): IO[ExitCode] = IO {
    import cats.effect.unsafe.implicits.global
    val result = prog[Fx.fx2[IO, ConfigEither]].runEither.unsafeRunSync
    result match {
      case Left(value)  => println(value.prettyPrint(2))
      case Right(value) => println("ok")
    }

  } >> IO.pure(ExitCode.Success)

}
