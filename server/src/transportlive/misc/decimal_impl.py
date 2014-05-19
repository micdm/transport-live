# coding=utf-8

from logging import getLogger

logger = getLogger(__name__)

try:
    from cdecimal import *
    logger.debug("Third-party decimal module found")
except ImportError:
    from decimal import *
    logger.warning("Cannot import third-party decimal module, calculations will be too slow!")

def normalize_coordinate(coordinate):
    return coordinate.quantize(Decimal("0.000001"))
