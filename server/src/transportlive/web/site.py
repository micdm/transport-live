from tornado.options import options
from tornado.template import Loader
from tornado.web import RequestHandler

class IndexHandler(RequestHandler):

    _template_loader = Loader(options.TEMPLATE_ROOT)

    def get(self):
        content = self._template_loader.load("index.html").generate()
        self.write(content)
