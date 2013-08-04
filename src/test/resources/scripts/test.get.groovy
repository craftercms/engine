def values = [:] 

params.each { name, value -> values[name] = value }
headers.each { name, value -> values[name] = value }
cookies.each { name, value -> values[name] = value }
sessionAttributes.each { name, value -> values[name] = value }

model["values"] = values
