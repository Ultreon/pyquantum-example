

def mod_main():
    from pyexample.constants import MOD_ID, LOGGER
    LOGGER.info("Loading Quantum Python Test Mod")


if __name__ == '__main__':
    try:
        mod_main()
    except Exception as e:
        throw = e.__traceback__
        while throw is not None:
            import sys

            print(throw.tb_lineno, throw.tb_frame.f_code.co_filename, throw.tb_frame.f_code.co_name, file=sys.stderr)
            throw = throw.tb_next

        print(e.__class__.__name__, e, file=sys.stderr)
