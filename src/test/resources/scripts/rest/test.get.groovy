package scripts.rest

def values = [:]

params.each { name, value -> values[name] = value }
headers.each { name, value -> values[name] = value }
cookies.each { name, value -> values[name] = value }

return values
