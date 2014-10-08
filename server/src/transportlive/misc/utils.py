# coding=utf-8

from decimal import Decimal

def normalize_coordinate(coordinate):
    return coordinate.quantize(Decimal("0.000001"))
