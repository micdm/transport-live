# coding=utf-8

from logging import getLogger

logger = getLogger(__name__)

try:
    from cdecimal import *
    logger.info("Third-party decimal module found")
except ImportError:
    from decimal import *
    logger.warning("Cannot import third-party decimal module, calculations will be too slow!")
