# coding=utf-8

class Vehicle(object):

    def __init__(self, vehicle_id):
        self.id = vehicle_id
        self.transport = None
        self.route = None
        self.marks = []

    @property
    def last_mark(self):
        return self.marks[-1] if len(self.marks) else None
